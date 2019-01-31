import Paper from 'material-ui/Paper';
import * as React from 'react';
import {mainContentPaperStyle} from '../../../app/themes';
import {Row, RowCenter} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {PageLayout} from '../../../containers/PageLayout';
import {PeriodContainer} from '../../../containers/PeriodContainer';
import {SummaryContainer} from '../../../containers/SummaryContainer';
import {translate} from '../../../services/translationService';
import {GatewayTabsContainer} from './GatewayTabsContainer';

export const GatewayPage = () => (
  <PageLayout>
    <Row className="space-between">
      <RowCenter>
        <MainTitle subtitle={translate('gateways')}>
          {translate('collection')}
        </MainTitle>
      </RowCenter>
      <Row>
        <SummaryContainer/>
        <PeriodContainer/>
      </Row>
    </Row>

    <Paper style={mainContentPaperStyle}>
      <GatewayTabsContainer/>
    </Paper>
  </PageLayout>
);
