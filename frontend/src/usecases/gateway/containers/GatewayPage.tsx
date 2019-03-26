import Paper from 'material-ui/Paper';
import * as React from 'react';
import {mainContentPaperStyle} from '../../../app/themes';
import {Row, RowCenter} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {PageLayout} from '../../../components/layouts/layout/PageLayout';
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
      </Row>
    </Row>

    <Paper style={mainContentPaperStyle}>
      <GatewayTabsContainer/>
    </Paper>
  </PageLayout>
);
