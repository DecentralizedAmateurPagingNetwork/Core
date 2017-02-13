#!/usr/bin/env python3

from argparse import ArgumentParser
import json

def parse_args(args):
    ap = ArgumentParser()
    ap.add_argument("-v", "--version", action="version", version="Version 0.1")
    ap.add_argument("input", nargs="?", help="input file name")

    return ap.parse_args(args=args)

def main(args):
    args = parse_args(args)

    with open(args.input, "r") as fin:
        data = json.load(fin)

    newdata = {}
    newdata["calls"] = data["calls"]
    newdata["news"] = data["news"]
    newdata["rubrics"] = {v["name"]: v for v in data["rubrics"]}
    newdata["callSigns"] = {v["name"]: v for v in data["callSigns"]}
    newdata["users"] = {v["name"]: v for v in data["users"]}
    newdata["nodes"] = {v["name"]: v for v in data["nodes"]}
    newdata["transmitters"] = {v["name"]: v for v in data["transmitters"]}
    newdata["transmitterGroups"] = {v["name"]: v for v in data["transmitterGroups"]}

    with open("State_new.json", "w") as fout:
        json.dump(newdata, fout, indent=2)

if __name__ == "__main__":
    main(None)
