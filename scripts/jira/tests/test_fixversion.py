import unittest
import logging
from fixversion import Jira
from unittest.mock import patch, Mock


class Foo():
    pass


class BasicTests(unittest.TestCase):
    jira = None
    args = Foo()
    args.jira_user = None
    args.jira_token = None
    args.version = None

    logger = logging.getLogger('nose')
    handler = logging.StreamHandler()
    formatter = logging.Formatter(
        '%(asctime)s %(name)-12s %(levelname)-8s %(message)s')
    handler.setFormatter(formatter)
    logger.addHandler(handler)
    logger.setLevel(logging.DEBUG)

    def setUp(self):
        self.args.jira_user = "fakeUser"
        self.args.jira_token = "fakeToken"
        self.args.version = "TestVersion 1.0"
        self.jira = Jira(
            version=self.args.version,
            jira_user=self.args.jira_user,
            jira_token=self.args.jira_token,
            log=self.logger
        )

    def test_create_jira_version_ok(self):
        mock_post_patcher = patch('fixversion.requests.post')
        mock_post = mock_post_patcher.start()
        mock_post.return_value.status_code = 201

        response = self.jira.create_jira_version()

        mock_post.stop()

        self.assertEqual(response, True)

    def test_create_jira_version_bad_request(self):
        mock_post_patcher = patch('fixversion.requests.post')
        mock_post = mock_post_patcher.start()
        mock_post.return_value.status_code = 400

        response = self.jira.create_jira_version()

        mock_post.stop()

        self.assertEqual(response, False)

    def test_add_issue_to_release_ok(self):
        mock_put_patcher = patch('fixversion.requests.put')
        mock_put = mock_put_patcher.start()
        mock_put.return_value.status_code = 204

        response = self.jira.add_issue_to_release("MVP-1")

        mock_put.stop()

        self.assertEqual(response, True)

    def test_add_issue_to_release_bad_request(self):
        mock_put_patcher = patch('fixversion.requests.put')
        mock_put = mock_put_patcher.start()
        mock_put.return_value.status_code = 400

        response = self.jira.add_issue_to_release("MVP-1")

        mock_put.stop()

        self.assertEqual(response, False)

    def test_add_issue_to_release_not_found(self):
        mock_put_patcher = patch('fixversion.requests.put')
        mock_put = mock_put_patcher.start()
        mock_put.return_value.status_code = 404

        response = self.jira.add_issue_to_release("MVP-1")

        mock_put.stop()

        self.assertEqual(response, False)

    def test_issues_to_release_default_values(self):
        expected_data = [
            'MVP-413', 'MVP-403', 'MVP-402', 'MVP-397', 'MVP-394',
            'MVP-387', 'MVP-383', 'MVP-342', 'MVP-308', 'MVP-232',
            'MVP-210', 'MVP-111', 'MVP-49', 'MVP-7'
        ]
        response_data = None
        with open('tests/fixtures/search_return_value.json') as test_data:
            response_data = test_data.read()

        mock_post_patcher = patch('fixversion.requests.post')
        mock_post = mock_post_patcher.start()
        mock_post.return_value = Mock(status_code=200)
        mock_post.return_value.text = response_data

        response = self.jira.issues_to_release()

        mock_post.stop()

        self.assertEqual(response, sorted(expected_data))

    def test_issues_to_release_empty_set(self):
        expected_data = []
        response_data = None
        with open('tests/fixtures/search_value_empty_set.json') as test_data:
                response_data = test_data.read()

        mock_post_patcher = patch('fixversion.requests.post')
        mock_post = mock_post_patcher.start()
        mock_post.return_value = Mock(status_code=200)
        mock_post.return_value.text = response_data

        response = self.jira.issues_to_release(max_result=1)

        mock_post.stop()

        self.assertEqual(response, sorted(expected_data))

    def test_issues_to_release_bad_request(self):
        mock_post_patcher = patch('fixversion.requests.post')
        mock_post = mock_post_patcher.start()
        mock_post.return_value = Mock(status_code=400)

        response = self.jira.issues_to_release()

        mock_post.stop()

        self.assertEqual(response, False)


if __name__ == "__main__":
    unittest.main()
