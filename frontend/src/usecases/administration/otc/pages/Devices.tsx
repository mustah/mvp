import Paper from 'material-ui/Paper';
import * as React from 'react';
import {paperStyle} from '../../../../app/themes';
import {AdminPageLayout} from '../../../../components/layouts/layout/PageLayout';
import {MainTitle} from '../../../../components/texts/Titles';
import {translate} from '../../../../services/translationService';
import {DeviceList} from '../components/DeviceList';

export const Devices = () => (
  <AdminPageLayout>
    <MainTitle>{translate('my devices')}</MainTitle>

    <Paper style={paperStyle}>
      <DeviceList/>
    </Paper>
  </AdminPageLayout>
);
