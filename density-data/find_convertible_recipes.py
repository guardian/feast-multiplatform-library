#!/usr/bin/env python3
import json
from recipe_api import load_all_recipes
from typing import Optional, List
import csv
import regex

class IngredientProps(object):
    def __init__(self, template:str):
        self.can_scale = False
        self.us_cust = False
        templateData = extract_json_template(template)
        if templateData is None:
            self.name = template
        else:
            self.name = templateData['ingredient']
            if templateData.get('scale', False):
                self.can_scale = True
            if templateData.get('usCust', False):
                self.us_cust = True
            
        
class RecipeProps(object):
    def __init__(self, csid:str, uid:str, title:str):
        self.csid = csid
        self.uid = uid
        self.title = title
        self.needs_scaling = False
        self.has_us_cust = False
        self.has_unknown_ingredients = False
        self.has_missing_keys = False

def get_ingredient_props(content):
    for s in content['ingredients']:
        for list in s['ingredientsList']:
            try:
                templ = list.get('template')
                if templ is None:
                    raise ValueError("Ingredient had no template string")
                yield IngredientProps(templ)
            except KeyError as e:
                print(f"ERROR processing template {templ} in recipe {content['id']}: missing key {e}")
                yield None

JsonFinder = regex.compile(r'\{(?:[^{}]|(?R))*\}')

def extract_json_template(templateString:str)-> Optional[dict]:
    match = JsonFinder.search(templateString)
    if match is None:
        return None
    try:
        return json.loads(match.group(0))
    except json.JSONDecodeError as e:
        raise ValueError(f"Error parsing JSON from template string: {e}")
    
def process_recipe(csid, content) -> Optional[RecipeProps]:
    try:
        props = list(get_ingredient_props(content))
        if len(props) == 0:
            print(f"Recipe {csid} has no ingredients, skipping")
            return

        recipe_props = RecipeProps(csid, content['id'], content['title'])
        recipe_props.needs_scaling = any(p is not None and p.can_scale for p in props)
        recipe_props.has_us_cust = any(p is not None and p.us_cust for p in props)
        recipe_props.has_unknown_ingredients = any(p is not None and p.name not in known_ingredients and p.us_cust for p in props)
        recipe_props.has_missing_keys = any(p is None for p in props)

        return recipe_props
    except ValueError as e:
        print(f"ERROR processing recipe {csid}: {e}")
        return
    
def load_densities(filepath:str) -> List[str]:
    with open(filepath, 'r') as f:
        content = json.loads(f.read())
        return [entry[2] for entry in content['values']]
    
### START MAIN
known_ingredients = load_densities('densities.json')
print(f"Loaded {len(known_ingredients)} known ingredients with densities")

fp_scaled_and_cust_known = open('scaled_and_cust_known.csv', 'w', newline='')
fp_scaled_and_cust_unknown = open('scaled_and_cust_unknown.csv', 'w', newline='')
fp_scaled_only = open('scaled_only.csv', 'w', newline='')
fp_no_scaling = open('no_scaling.csv', 'w', newline='')
writer_scaled_and_cust_known = csv.writer(fp_scaled_and_cust_known)
writer_scaled_and_cust_unknown = csv.writer(fp_scaled_and_cust_unknown)
writer_scaled_only = csv.writer(fp_scaled_only)
writer_no_scaling = csv.writer(fp_no_scaling)

for csid, txt in load_all_recipes(base_uri='https://recipes.guardianapis.com'):
    content = json.loads(txt)
    props = process_recipe(csid, content)
    if props is not None:
        if props.needs_scaling and props.has_us_cust and not props.has_unknown_ingredients:
            writer_scaled_and_cust_known.writerow([props.csid, props.uid, props.title, props.has_missing_keys])
        elif props.needs_scaling and props.has_us_cust:
            writer_scaled_and_cust_unknown.writerow([props.csid, props.uid, props.title, props.has_missing_keys])
        elif props.needs_scaling:
            writer_scaled_only.writerow([props.csid, props.uid, props.title, props.has_missing_keys])
        else:
            writer_no_scaling.writerow([props.csid, props.uid, props.title, props.has_missing_keys])