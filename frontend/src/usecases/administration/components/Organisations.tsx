import Paper from 'material-ui/Paper';
import * as React from 'react';
import {paperStyle} from '../../../app/themes';
import {MainTitle} from '../../../components/texts/Titles';
import {AdminPageLayout} from '../../../components/layouts/layout/PageLayout';
import {translate} from '../../../services/translationService';
import {OrganisationsContainer} from '../containers/OrganisationsContainer';

export const Organisations = () => (
  <AdminPageLayout>
    <MainTitle>{translate('organisations')}</MainTitle>

    <Paper style={paperStyle}>
      <OrganisationsContainer/>
    </Paper>
  </AdminPageLayout>
);
