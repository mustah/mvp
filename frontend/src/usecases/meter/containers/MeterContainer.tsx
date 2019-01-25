import Paper from 'material-ui/Paper';
import * as React from 'react';
import {mainContentPaperStyle} from '../../../app/themes';
import {Row, RowCenter} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {MvpPageContainer} from '../../../containers/MvpPageContainer';
import {PeriodContainer} from '../../../containers/PeriodContainer';
import {SummaryContainer} from '../../../containers/SummaryContainer';
import {translate} from '../../../services/translationService';
import {MeterTabsContainer} from './MeterTabsContainer';

export const MeterContainer = () => (
  <MvpPageContainer>
    <Row className="space-between">
      <RowCenter>
        <MainTitle>
          {translate('meter', {count: 2})}
        </MainTitle>
      </RowCenter>
      <Row>
        <SummaryContainer/>
        <PeriodContainer/>
      </Row>
    </Row>

    <Paper style={mainContentPaperStyle}>
      <MeterTabsContainer/>
    </Paper>
  </MvpPageContainer>
);
