import Paper from 'material-ui/Paper';
import * as React from 'react';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {translate} from '../../../services/translationService';
import {paperStyle} from '../../app/themes';
import {Row} from '../../common/components/layouts/row/Row';
import {PageContainer} from '../../common/containers/PageContainer';
import {PeriodContainer} from '../../common/containers/PeriodContainer';
import {SummaryContainer} from '../../common/containers/SummaryContainer';
import CollectionTabsContainer from '../containers/CollectionTabsContainer';
import {MainTitle} from '../../common/components/texts/Titles';

type Props = InjectedAuthRouterProps;

export class Collection extends React.Component<Props> {
  render() {
    return (
      <PageContainer>
        <Row className="space-between">
          <MainTitle subtitle={translate('gateway')}>
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
      </PageContainer>
    );
  }
}
