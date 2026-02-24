import csv

def sanitise_string(text:str) -> str:
    return text.strip('* ')

def load_groupings(filepath:str) -> dict:
    with open(filepath) as f:
        reader = csv.reader(f)
        available_groupings = [row for row in reader if row[6]=='HIGH' or row[6]=='MEDIUM']

    return dict(
        [(sanitise_string(row[1]), sanitise_string(row[3])) for row in available_groupings]
    )