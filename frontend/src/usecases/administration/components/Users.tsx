import Paper from 'material-ui/Paper';
import * as React from 'react';
import {paperStyle} from '../../../app/themes';
import {PageTitle} from '../../../components/texts/Titles';
import {AdminPageComponent} from '../../../containers/PageComponent';
import {translate} from '../../../services/translationService';
import {UserAdministrationContainer} from '../containers/UserAdministrationContainer';

export const Users = () => (
  <AdminPageComponent>
    <PageTitle>
      {translate('users')}
    </PageTitle>

    <Paper style={paperStyle}>
      <UserAdministrationContainer/>
    </Paper>
  </AdminPageComponent>
);
