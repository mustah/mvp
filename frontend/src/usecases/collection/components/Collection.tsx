import Paper from 'material-ui/Paper';
import * as React from 'react';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {translate} from '../../../services/translationService';
import {Row} from '../../common/components/layouts/row/Row';
import {MainTitle} from '../../common/components/texts/Title';
import {PageContainer} from '../../common/containers/PageContainer';
import CollectionTabsContainer from '../containers/CollectionTabsContainer';
import {paperStyle} from '../../app/themes';

type Props = InjectedAuthRouterProps;

export class Collection extends React.Component<Props> {
  render() {
    return (
      <PageContainer>
        <Row className="space-between">
          <MainTitle>{translate('collection')}</MainTitle>
        </Row>

        <Paper style={paperStyle}>
          <CollectionTabsContainer/>
        </Paper>
      </PageContainer>
    );
  }
}
