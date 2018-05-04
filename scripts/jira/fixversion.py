#!/bin/env python3

import argparse
import datetime
import json
import logging
import requests
import sys


class Jira:
    version = None
    jira_user = None
    jira_token = None
    jira_url = 'https://elvaco.atlassian.net/rest/api/2'
    jira_project_name = 'MVP'
    jira_project_id = 10000
    release_date = datetime.datetime.now().strftime("%Y-%m-%d")
    headers = {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
    }

    def __init__(self, version, jira_user=jira_user,
                 jira_token=jira_token, log=None):

            self.log = log if log else lambda x: None
            self.version = version
            self.jira_user = jira_user
            self.jira_token = jira_token

    def create_jira_version(self):
        url = self.jira_url + '/version'
        payload = {
            'description': '',
            'name': self.version,
            'archived': False,
            'released': True,
            'releaseDate': self.release_date,
            'project': self.jira_project_name,
            'projectId': self.jira_project_id
        }
        r = requests.post(
            url,
            auth=(self.jira_user, self.jira_token),
            data=json.dumps(payload),
            headers=self.headers
        )

        if r.status_code != 201:
            self.log.critical(r.text)
            self.log.critical("Status Code: {}".format(r.status_code))
            return False
        return True

    def issues_to_release(self, offset=0, max_result=100):
        issues = []

        url = self.jira_url + '/search'
        jql_query = "issuetype in (Bug, Story) "
        jql_query += "AND project = MVP "
        jql_query += "AND fixVersion is EMPTY "
        jql_query += "AND status = Done "
        jql_query += "AND resolution = Done"

        payload = {
            'jql': jql_query,
            'startAt': offset,
            'maxResults': max_result,
            'fields': ['id'],
            'fieldsByKeys': False
        }
        r = requests.post(
            url,
            auth=(self.jira_user, self.jira_token),
            data=json.dumps(payload),
            headers=self.headers
        )

        if r.status_code != 200:
            self.log.critical(r.text)
            self.log.critical("Status Code: {}".format(r.status_code))
            return False

        data = json.loads(r.text)
        for element in data['issues']:
            issues.append(element['key'])

        if (max_result + offset) < data['total']:
            issues += self.issues_to_release(
                offset=(offset+max_result),
                max_result=max_result)

        return sorted(issues)

    def add_issue_to_release(self, issueKey):
        url = self.jira_url + '/issue/' + issueKey
        payload = {
            "update": {
                "fixVersions": [
                    {"set": [{"name": self.version}]}
                ]
            }
        }

        r = requests.put(
            url,
            auth=(self.jira_user, self.jira_token),
            data=json.dumps(payload),
            headers=self.headers
        )

        if r.status_code != 204:
            self.log.critical(r.text)
            self.log.critical("Status Code: {}".format(r.status_code))
            return False

        self.log.info("Adding issue: {} to release: {}".format(
            issueKey, self.version))
        return True


def main(args, logger):
    exit_code = 0

    jira = Jira(
        version=args.version,
        jira_user=args.jira_user,
        jira_token=args.jira_token,
        log=logger
    )

    logger.info("Searching for issues for release: {}".format(jira.version))
    issues = jira.issues_to_release()
    if not issues:
        logger.critical("No issues found, skipping")
        sys.exit(1)

    logger.info("Creating release: {}".format(jira.version))
    if not jira.create_jira_version():
        sys.exit(1)

    for issue in issues:
        response = jira.add_issue_to_release(issue)
        if not response:
            exit_code = 1

    sys.exit(exit_code)


if __name__ == "__main__":
    logger = logging.getLogger('fixversion')
    handler = logging.StreamHandler()
    formatter = logging.Formatter(
        '%(asctime)s %(name)-12s %(levelname)-8s %(message)s')
    handler.setFormatter(formatter)
    logger.addHandler(handler)
    logger.setLevel(logging.DEBUG)

    parser = argparse.ArgumentParser()
    parser.add_argument("--version",
                        help="Version name to create in Jira", required=True)

    parser.add_argument("--jira_user",
                        help="Username for Jira API", required=True)

    parser.add_argument("--jira_token",
                        help="Token for Jira API", required=True)

    args = parser.parse_args()

    main(args, logger)
