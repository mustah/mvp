import Paper from 'material-ui/Paper';
import * as React from 'react';
import {paperStyle} from '../../../app/themes';
import {Row} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {PageComponent} from '../../../containers/PageComponent';
import {translate} from '../../../services/translationService';
import {UserAdministrationContainer} from '../containers/UserAdministrationContainer';

export const Administration = () => {
  return (
    <PageComponent isSideMenuOpen={false}>
      <Row className="space-between">
        <MainTitle>
          {translate('administration')}
        </MainTitle>
      </Row>

      <Paper style={paperStyle}>
        <UserAdministrationContainer/>
      </Paper>
    </PageComponent>
  );
};
