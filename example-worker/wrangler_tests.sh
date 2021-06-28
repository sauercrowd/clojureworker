#!/bin/bash
set -ex

wrangler preview -u https://example.com/api/string-as-html --headless | grep 'cool response'
wrangler preview -u https://example.com/api/map-as-json --headless | grep '{"hello":1}'
wrangler preview -u https://example.com/api/ping --headless post ping! | grep 'ping!'
