import requests
from typing import Optional


def load_index(base_uri:str):
    """
    Generator that returns index entries for each recipe
    """
    server_response = requests.get(f"{base_uri}/v3/index.json")
    if server_response.status_code != 200:
        print(f'ERROR {base_uri} returned an error code {server_response.status_code}')
        exit(1)
    
    parsed_content = server_response.json()
    print(f'INFO Found {len(parsed_content["recipes"])} recipes')
    for entry in parsed_content["recipes"]:
        yield entry


def load_all_recipes(base_uri:str):
    """
    Generator that yields every recipe from the system in raw format
    """
    for index_entry in load_index(base_uri):
        server_response = requests.get(f'{base_uri}/content/{index_entry["checksum"]}')
        if server_response.status_code==200:
            yield index_entry["checksum"], server_response.text
        else:
            print(f'ERROR The server returned {server_response.status_code} when loading a recipe from {index_entry["capiArticleId"]}')


class RecipeDoesNotExist(Exception):
    pass


class PermissionDenied(Exception):
    pass


def load_recipe_by_csid(base_uri:str, recipe_checksum: str, session:Optional[requests.Session]=None)->dict:
    """
    Load the recipe with the given checksum id
    :param base_uri:
    :param recipe_checksum:
    :param session: pass a requests.Session() object to enable re-use
    :return:
    """
    if session is None:
        session = requests.Session()

    server_response = session.get(f'{base_uri}/content/{recipe_checksum}', stream=False)
    if server_response.status_code==200:
        return server_response.json()
    elif server_response.status_code==403 or server_response.status_code==404:
        raise RecipeDoesNotExist(recipe_checksum)


def lookup_recipe_by_uuid(base_uri:str, api_key:str, uuid:str, session:Optional[requests.Session]=None)->str:
    """
    Look up the recipe with the given UUID (from core recipe api, not search)
    :param base_uri:
    :param api_key:
    :param uuid:
    :return: the checksum ID of the given recipe
    """
    if api_key is None:
        raise PermissionDenied("You must specify an API key")

    if session is None:
        session = requests.Session()

    server_response = session.get(f'{base_uri}/api/content/by-uid/{uuid}',
                                   headers={
                                       'x-api-key': api_key
                                   },
                                  stream=False,
                                  allow_redirects=False)
    if server_response.status_code==302:
        loc = server_response.headers.get("location")
        if loc is None:
            raise RuntimeError("Got valid record but no location! What's going on?")
        else:
            parts = loc.split("/")
            return parts[-1]
    elif server_response.status_code==404:
        raise RecipeDoesNotExist(uuid)
    elif server_response.status_code==403:
        raise PermissionDenied(api_key)
    else:
        data = server_response.text
        raise RuntimeError(f'Server error {server_response.status_code} during lookup: {data}')


def get_sponsorship_by_csid(base_uri:str, csid:str, session:Optional[requests.Session])->Optional[list]:
    recep = load_recipe_by_csid(base_uri, csid, session)
    return recep.get("sponsors")