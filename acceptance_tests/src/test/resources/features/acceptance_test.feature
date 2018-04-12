Feature:
    As a developer
    I want to be able to login
    So that I see that the site works

    Scenario Outline: Login to MVP
        Given I am on the login page
        When I login as user '<user>' and password '<password>'
        Then I should see '<context>'

    Examples:
        |user|password|context|
        |blackbox-test@elvaco.se|Räksmörgås|urval|

    Scenario Outline: Deny login to MVP
        Given I am on the login page
        When I login as user '<user>' and password '<password>'
        Then I should not see '<context>'

    Examples:
        |user|password|context|
        |blackbox-test@elvaco.se|nope|urval|
