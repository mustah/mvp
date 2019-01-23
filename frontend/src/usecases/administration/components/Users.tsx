import Paper from 'material-ui/Paper';
import * as React from 'react';
import {paperStyle} from '../../../app/themes';
import {PageTitle} from '../../../components/texts/Titles';
import {AdminPageComponent} from '../../../containers/PageComponent';
import {translate} from '../../../services/translationService';
import {UsersContainer} from '../containers/UsersContainer';

export const Users = () => (
  <AdminPageComponent>
    <PageTitle>
      {translate('users')}
    </PageTitle>

    <Paper style={paperStyle}>
      <UsersContainer/>
    </Paper>
  </AdminPageComponent>
);
