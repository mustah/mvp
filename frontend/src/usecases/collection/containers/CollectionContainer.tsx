import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../../../reducers/index';
import {SelectionOverview} from '../../common/components/selectionoverview/SelectionOverview';
import {Column} from '../../layouts/components/column/Column';
import {Content} from '../../layouts/components/content/Content';
import {Layout} from '../../layouts/components/layout/Layout';
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
        <SelectionOverview title={'Allt'}/>
        <Content>
          <CollectionOverview/>
          <div className="button" onClick={fetchCollections}>COLLECTIONS</div>
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
