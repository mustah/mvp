import Paper from 'material-ui/Paper';
import * as React from 'react';
import {mainContentPaperStyle} from '../../../app/themes';
import {Row, RowSpaceBetween} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {PageLayout} from '../../../containers/PageLayout';
import {PeriodContainer} from '../../../containers/PeriodContainer';
import {SummaryContainer} from '../../../containers/SummaryContainer';
import {translate} from '../../../services/translationService';
import {MeterTabsContainer} from './MeterTabsContainer';

export const MetersPage = () => (
  <PageLayout>
    <RowSpaceBetween>
      <MainTitle>{translate('meter', {count: 2})}</MainTitle>
      <Row>
        <SummaryContainer/>
        <PeriodContainer/>
      </Row>
    </RowSpaceBetween>

    <Paper style={{...mainContentPaperStyle, paddingTop: 8}}>
      <MeterTabsContainer/>
    </Paper>
  </PageLayout>
);
