#!/bin/bash
set -ex

wrangler preview -u https://example.com/v1/api/test --headless | grep 'cool response'
wrangler preview -u https://example.com/v1/api/ping --headless post ping! | grep 'ping!'
