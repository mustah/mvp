import Paper from 'material-ui/Paper';
import * as React from 'react';
import {paperStyle} from '../../../app/themes';
import {AdminPageLayout} from '../../../components/layouts/layout/PageLayout';
import {MainTitle} from '../../../components/texts/Titles';
import {translate} from '../../../services/translationService';
import {UseCases} from '../../../types/Types';
import {UsersContainer} from '../containers/UsersContainer';

export const Users = () => (
  <AdminPageLayout>
    <MainTitle>{translate('users')}</MainTitle>

    <Paper style={paperStyle}>
      <UsersContainer useCase={UseCases.admin}/>
    </Paper>
  </AdminPageLayout>
);
