import Paper from 'material-ui/Paper';
import * as React from 'react';
import {paperStyle} from '../../../app/themes';
import {MainTitle} from '../../../components/texts/Titles';
import {AdminPageLayout} from '../../../components/layouts/layout/PageLayout';
import {translate} from '../../../services/translationService';
import {UsersContainer} from '../containers/UsersContainer';

export const Users = () => (
  <AdminPageLayout>
    <MainTitle>{translate('users')}</MainTitle>

    <Paper style={paperStyle}>
      <UsersContainer/>
    </Paper>
  </AdminPageLayout>
);
