import Paper from 'material-ui/Paper';
import * as React from 'react';
import {paperStyle} from '../../../app/themes';
import {Row} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {MvpPageContainer} from '../../../containers/MvpPageContainer';
import {PeriodContainer} from '../../../containers/PeriodContainer';
import {SummaryContainer} from '../../../containers/SummaryContainer';
import {translate} from '../../../services/translationService';
import {CollectionTabsContainer} from '../containers/CollectionTabsContainer';

export const Collection = () => (
  <MvpPageContainer>
    <Row className="space-between">
      <MainTitle subtitle={translate('gateways')}>
        {translate('collection')}
      </MainTitle>
      <Row>
        <SummaryContainer/>
        <PeriodContainer/>
      </Row>
    </Row>

    <Paper style={paperStyle}>
      <CollectionTabsContainer/>
    </Paper>
  </MvpPageContainer>
);
