#!/usr/bin/env python3

import csv
from argparse import ArgumentParser
from typing import List, Optional
import logging
import sys
import json
from datetime import datetime

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class DensityEntry(object):
    def __init__(self, row:Optional[List[str]]):
        """
        Initialises a new object from a row of CSV
        :param row:
        """
        if row is None:
            self.id = 0
            self.name = ""
            self.normalised_name = ""
            self.density = 0.0
            self.source = ""
            return

        if len(row)<4:
            raise ValueError("row did not have enough entries, expected at least 4")
        if len(row)>5:
            logger.warning("got more rows than expected on input data, extras will be ignored")

        self.id = int(row[0])
        self.name = row[1]
        self.normalised_name = row[2]
        self.density = float(row[3])
        self.source = ""

### START MAIN
parser = ArgumentParser()
parser.add_argument("--input", "-i", type=str, help="input CSV file")
parser.add_argument("--continue-on-incomplete", "-c", action="store_true", help="don't stop if some data lines were not parseable")
parser.add_argument("--output", "-o", type=str, default="output.json", help="json file to output")
args = parser.parse_args()

entries = []
some_did_fail = False

if args.input is None:
    logger.error("You must specify an input file.  Use --help to see the options.")

with open(args.input) as f:
    reader = csv.reader(f)
    for idx, row in enumerate(reader):
        try:
            entries.append(DensityEntry(row))
        except ValueError as e:
            if idx==0:
                logger.info(f"Skipping possible header row {row}")
                continue
            logger.warning(f"Could not parse row {idx}: {e}")
            some_did_fail = True

if some_did_fail and not args.continue_on_incomplete:
    logger.error("The data file was incomplete, see errors above")
    sys.exit(1)

if len(entries) ==0:
    logger.error("Could not load any densities")
    sys.exit(1)

logger.info(f"Loaded {len(entries)} densities")

result = {
    'prepared_at': datetime.now().isoformat(),
    'key': ["id","name","normalised_name","density"],
    'values': [[e.id, e.name, e.normalised_name, e.density] for e in entries]
}

with open(args.output, "w") as f:
    f.write(json.dumps(result))
