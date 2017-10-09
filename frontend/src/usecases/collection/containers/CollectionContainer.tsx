import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../../../reducers/index';
import {translate} from '../../../services/translationService';
import {Image} from '../../common/components/images/Image';
import {SelectionOverview} from '../../common/components/selection-overview/SelectionOverview';
import {Title} from '../../common/components/texts/Title';
import {Column} from '../../common/components/layouts/column/Column';
import {Content} from '../../common/components/layouts/content/Content';
import {Layout} from '../../common/components/layouts/layout/Layout';
import {fetchCollections} from '../collectionActions';
import {CollectionOverview} from '../components/CollectionOverview';
import {CollectionState} from '../models/Collections';

export interface CollectionContainerProps {
  fetchCollections: () => any;
  collection: CollectionState;
}

const CollectionContainer = (props: CollectionContainerProps & InjectedAuthRouterProps) => {
  const {fetchCollections} = props;
  return (
    <Layout>
      <Column className="flex-1">
        <SelectionOverview title={translate('all')}/>
        <Content>
          <CollectionOverview/>
          <Image src="usecases/collection/img/collections-errors-warnings.png"/>

          <Title>{translate('gateway')}</Title>
          <Image src="usecases/collection/img/gateways.png"/>

          <div className="button" onClick={fetchCollections}>{translate('collection')}</div>
        </Content>
      </Column>
    </Layout>
  );
};

const mapStateToProps = (state: RootState) => {
  const {collection} = state;
  return {
    collection,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  fetchCollections,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(CollectionContainer);
