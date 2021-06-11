#!/bin/bash
set -ex

wrangler preview -u https://example.com/api/test --headless | grep 'cool response'
wrangler preview -u https://example.com/api/ping --headless post ping! | grep 'ping!'
