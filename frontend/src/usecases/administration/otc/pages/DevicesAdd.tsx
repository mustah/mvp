import Paper from 'material-ui/Paper';
import * as React from 'react';
import {paperStyle} from '../../../../app/themes';
import {AdminPageLayout} from '../../../../components/layouts/layout/PageLayout';
import {MainTitle} from '../../../../components/texts/Titles';
import {translate} from '../../../../services/translationService';
import {MoreToCome} from '../components/MoreToCome';

export const DevicesAdd = () => (
  <AdminPageLayout>
    <MainTitle>{translate('add device')}</MainTitle>

    <Paper style={paperStyle}>
      <MoreToCome/>
    </Paper>
  </AdminPageLayout>
);
