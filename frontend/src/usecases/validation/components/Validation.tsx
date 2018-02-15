import Paper from 'material-ui/Paper';
import * as React from 'react';
import {paperStyle} from '../../../app/themes';
import {Row} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {MvpPageContainer} from '../../../containers/MvpPageContainer';
import {PeriodContainer} from '../../../containers/PeriodContainer';
import {SummaryContainer} from '../../../containers/SummaryContainer';
import {translate} from '../../../services/translationService';
import {ValidationTabsContainer} from '../containers/ValidationTabsContainer';

export const Validation = () => {

  // TODO: Fix so that SummaryContainer don't break.
  return (
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

      <Paper style={paperStyle}>
        <ValidationTabsContainer/>
      </Paper>
    </MvpPageContainer>
  );
};
