import Paper from 'material-ui/Paper';
import * as React from 'react';
import {paperStyle} from '../../../../app/themes';
import {AdminPageLayout} from '../../../../components/layouts/layout/PageLayout';
import {MainTitle} from '../../../../components/texts/Titles';
import {translate} from '../../../../services/translationService';
import {Titled, WithChildren} from '../../../../types/Types';
import {BatchReferencesContent} from '../batch-references/components/BatchReferencesContent';
import {BatchReferenceFormContainer} from '../batch-references/containers/BatchReferenceFormContainer';

type Props = WithChildren & Titled;

const PageLayout = ({children, title}: Props) => (
  <AdminPageLayout>
    <MainTitle>{title}</MainTitle>
    <Paper style={paperStyle}>
      {children}
    </Paper>
  </AdminPageLayout>
);

export const BatchReferencesCreate = () => (
  <PageLayout title={translate('create batch reference')}>
    <BatchReferenceFormContainer/>
  </PageLayout>
);

export const BatchReferences = () => (
  <PageLayout title={translate('batch references')}>
    <BatchReferencesContent/>
  </PageLayout>
);
