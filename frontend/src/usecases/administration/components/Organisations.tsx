import Paper from 'material-ui/Paper';
import * as React from 'react';
import {paperStyle} from '../../../app/themes';
import {MainTitle} from '../../../components/texts/Titles';
import {AdminPageComponent} from '../../../containers/PageComponent';
import {translate} from '../../../services/translationService';
import {OrganisationsContainer} from '../containers/OrganisationsContainer';

export const Organisations = () => (
  <AdminPageComponent>
    <MainTitle>{translate('organisations')}</MainTitle>

    <Paper style={paperStyle}>
      <OrganisationsContainer/>
    </Paper>
  </AdminPageComponent>
);
