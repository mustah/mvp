import Paper from 'material-ui/Paper';
import * as React from 'react';
import {paperStyle} from '../../../../app/themes';
import {AdminPageLayout} from '../../../../components/layouts/layout/PageLayout';
import {MainTitle} from '../../../../components/texts/Titles';
import {translate} from '../../../../services/translationService';
import {MeterDefinitionsContainer} from '../containers/MeterDefinitionsContainer';

export const MeterDefinitions = () => (
  <AdminPageLayout>
    <MainTitle>{translate('meter definitions')}</MainTitle>

    <Paper style={paperStyle}>
      <MeterDefinitionsContainer/>
    </Paper>
  </AdminPageLayout>
);
