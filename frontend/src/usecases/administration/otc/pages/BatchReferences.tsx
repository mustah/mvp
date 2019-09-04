import Paper from 'material-ui/Paper';
import * as React from 'react';
import {paperStyle} from '../../../../app/themes';
import {AdminPageLayout} from '../../../../components/layouts/layout/PageLayout';
import {MainTitle} from '../../../../components/texts/Titles';
import {translate} from '../../../../services/translationService';
import {BatchReferenceList} from '../components/BatchReferenceList';

export const BatchReferences = () => (
  <AdminPageLayout>
    <MainTitle>{translate('batch references')}</MainTitle>

    <Paper style={paperStyle}>
      <BatchReferenceList/>
    </Paper>
  </AdminPageLayout>
);