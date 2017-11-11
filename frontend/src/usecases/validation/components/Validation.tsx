import Paper from 'material-ui/Paper';
import * as React from 'react';
import {translate} from '../../../services/translationService';
import {paperStyle} from '../../app/themes';
import {Row} from '../../common/components/layouts/row/Row';
import {MainTitle} from '../../common/components/texts/Titles';
import {PageContainer} from '../../common/containers/PageContainer';
import {SummaryContainer} from '../../common/containers/SummaryContainer';
import ValidationTabsContainer from '../containers/ValidationTabsContainer';

export const Validation = () => {
  return (
    <PageContainer>
      <Row className="space-between">
        <MainTitle>{translate('validation')}</MainTitle>
        <SummaryContainer/>
      </Row>

      <Paper style={paperStyle}>
        <ValidationTabsContainer/>
      </Paper>

    </PageContainer>
  );
};
