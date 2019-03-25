# encoding: utf-8
import json
import os
import requests
import sys

def get_env(*variables):
    values = []
    for variable in variables:
        value = os.environ.get(variable)
        if not value:
            raise RuntimeError('Missing environment variable ' + variable)
        values.append(value)
    return tuple(i for i in values)


def output_for(response, ms_limit):
    assert isinstance(response, requests.Response)
    assert isinstance(ms_limit, int)

    actual_ms = response.elapsed.microseconds / 1000
    status_ok = actual_ms < ms_limit and response.ok

    return (
        response.request.url,
        ('OK' if status_ok else 'FAILED', response.status_code, str(actual_ms) + 'ms', str(ms_limit) + 'ms', len(json.loads(response.content))),
        status_ok
    )


def perform_requests(user, password, mvp_url):
    assert all([user, password, mvp_url])

    fail_if_slower_than_ms = {
        '/api/v1/authenticate': 1200,
        '/api/v1/dashboards': 1200,
        '/api/v1/map-markers/meters': 1200,
        '/api/v1/meters': 1200,
        '/api/v1/meters?after=2018-12-06T00%3A00%3A00.000%2B01%3A00&before=2018-12-07T00%3A00%3A00.000%2B01%3A00': 1200,
        '/api/v1/meters?after=2018-12-06T00%3A00%3A00.000%2B01%3A00&before=2018-12-07T00%3A00%3A00.000%2B01%3A00&city=sverige%2Cmalm%C3%B6': 1200,
        '/api/v1/meters?threshold=Return%20temperature%20%3E%201%20%C2%B0C&after=2018-12-06T00%3A00%3A00.000%2B01%3A00&before=2018-12-07T00%3A00%3A00.000%2B01%3A00': 1200,
        '/api/v1/summary/meters': 1200,
        '/api/v1/user/selections': 1200,
    }

    failures = 0
    output_format = '{0:<6}  {1:>9}  {2:>11}  {3:>11}  {4:>11}'
    header = ('Status', 'HTTP code', 'Actual time', 'Time limit', 'JSON length')
    print("\n" + output_format.format(*header) + "\n")

    for path in fail_if_slower_than_ms:
        response = requests.get(mvp_url + path, auth=(user, password))
        ms_limit = fail_if_slower_than_ms[path]

        url, columns, status_ok = output_for(response, ms_limit)
        rows = output_format.format(*columns)
        print(url)
        print(rows + "\n")
        failures += int(not status_ok)

    return failures


if __name__ == '__main__':
    test_failures = perform_requests(*get_env('USER_EMAIL', 'USER_PASSWORD', 'MVP_URL'))
    print("Success" if not test_failures else "\nFailures: " + str(test_failures))
    sys.exit(test_failures)
