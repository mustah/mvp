import Paper from 'material-ui/Paper';
import * as React from 'react';
import {mainContentPaperStyle} from '../../../app/themes';
import {Row} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {MvpPageContainer} from '../../../containers/MvpPageContainer';
import {PeriodContainer} from '../../../containers/PeriodContainer';
import {SummaryContainer} from '../../../containers/SummaryContainer';
import {translate} from '../../../services/translationService';
import {ValidationTabsContainer} from '../containers/ValidationTabsContainer';

export const Validation = () => (
  <MvpPageContainer>
    <Row className="space-between">
      <MainTitle subtitle={translate('meter', {count: 2})}>
        {translate('validation')}
      </MainTitle>
      <Row>
        <SummaryContainer/>
        <PeriodContainer/>
      </Row>
    </Row>

    <Paper style={mainContentPaperStyle}>
      <ValidationTabsContainer/>
    </Paper>
  </MvpPageContainer>
);
