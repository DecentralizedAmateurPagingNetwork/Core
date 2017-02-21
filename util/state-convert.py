#!/usr/bin/env python3

from argparse import ArgumentParser
import json

def parse_args(args):
    ap = ArgumentParser()
    ap.add_argument("-v", "--version", action="version", version="Version 0.1")
    ap.add_argument("input", nargs="?", help="input file name")

    return ap.parse_args(args=args)

def addMissingAttributes(data):
    for v in data.values():
        v["status"] = "OFFLINE"
        v["antennaType"] = "OMNI"
        v["usage"] = "WIDERANGE"
        v["authKey"] = "test1234"

def addData(data, newdata, key):
    newdata[key] = {v["name"].lower(): v for v in data[key]}

    for v in newdata[key].values():
        v["name"] = v["name"].lower()

        if "ownerNames" in v.keys():
            v["ownerNames"] = [k.lower() for k in v["ownerNames"]]

        if "rubricName" in v.keys():
            v["rubricName"] = v["rubricName"].lower()

        if "transmitterNames" in v.keys():
            v["transmitterNames"] = [k.lower() for k in v["transmitterNames"]]

        if "transmitterGroupNames" in v.keys():
            v["transmitterGroupNames"] = [k.lower() for k in v["transmitterGroupNames"]]

        if "callSignNames" in v.keys():
            v["callSignNames"] = [k.lower() for k in v["callSignNames"]]


def main(args):
    args = parse_args(args)

    with open(args.input, "r") as fin:
        data = json.load(fin)

    newdata = {}
    newdata["calls"] = data["calls"]
    for v in newdata["calls"]:
        v["ownerName"] = v["ownerName"].lower()
        v["callSignNames"] = [k.lower() for k in v["callSignNames"]]
        v["transmitterGroupNames"] = [k.lower() for k in v["transmitterGroupNames"]]

    newdata["news"] = data["news"]
    for v in newdata["news"]:
        v["rubricName"] = v["rubricName"].lower()

    addData(data, newdata, "rubrics")
    addData(data, newdata, "callSigns")
    addData(data, newdata, "users")
    addData(data, newdata, "nodes")
    addData(data, newdata, "transmitters")
    addData(data, newdata, "transmitterGroups")

    addMissingAttributes(newdata["transmitters"])

    with open("State_new.json", "w") as fout:
        json.dump(newdata, fout, indent=2)

if __name__ == "__main__":
    main(None)
