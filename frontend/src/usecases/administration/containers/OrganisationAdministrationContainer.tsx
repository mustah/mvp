import Paper from 'material-ui/Paper';
import * as React from 'react';
import {paperStyle} from '../../../app/themes';
import {OrganisationsActionsDropdown} from '../../../components/actions-dropdown/OrganisationsActionsDropdown';
import {Row, RowRight} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {PageComponent} from '../../../containers/PageComponent';
import {translate} from '../../../services/translationService';

export const OrganisationAdministrationContainer = () => (
  <PageComponent isSideMenuOpen={false}>
    <Row className="space-between">
      <MainTitle>
        {translate('organisations')}
      </MainTitle>
    </Row>

    <Paper style={paperStyle}>
      <RowRight>
        <OrganisationsActionsDropdown />
      </RowRight>
    </Paper>
  </PageComponent>
);
