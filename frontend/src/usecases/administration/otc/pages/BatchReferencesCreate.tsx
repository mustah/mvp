import Paper from 'material-ui/Paper';
import * as React from 'react';
import {paperStyle} from '../../../../app/themes';
import {AdminPageLayout} from '../../../../components/layouts/layout/PageLayout';
import {MainTitle} from '../../../../components/texts/Titles';
import {translate} from '../../../../services/translationService';
import {MoreToCome} from '../components/MoreToCome';

export const BatchReferencesCreate = () => (
  <AdminPageLayout>
    <MainTitle>{translate('create batch reference')}</MainTitle>

    <Paper style={paperStyle}>
      <MoreToCome/>
    </Paper>
  </AdminPageLayout>
);
