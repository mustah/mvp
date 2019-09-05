import Paper from 'material-ui/Paper';
import * as React from 'react';
import {paperStyle} from '../../../../app/themes';
import {AdminPageLayout} from '../../../../components/layouts/layout/PageLayout';
import {MainTitle} from '../../../../components/texts/Titles';
import {translate} from '../../../../services/translationService';
import {DevicesContent} from '../components/DevicesContent';

export const Devices = () => (
  <AdminPageLayout>
    <MainTitle>{translate('my devices')}</MainTitle>

    <Paper style={paperStyle}>
      <DevicesContent/>
    </Paper>
  </AdminPageLayout>
);
