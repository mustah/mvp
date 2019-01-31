import Paper from 'material-ui/Paper';
import * as React from 'react';
import {mainContentPaperStyle} from '../../../app/themes';
import {Row, RowCenter} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {PageComponent} from '../../../containers/PageComponent';
import {PeriodContainer} from '../../../containers/PeriodContainer';
import {SummaryContainer} from '../../../containers/SummaryContainer';
import {translate} from '../../../services/translationService';
import {GatewayTabsContainer} from './GatewayTabsContainer';

export const GatewayPage = () => (
  <PageComponent>
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
  </PageComponent>
);
