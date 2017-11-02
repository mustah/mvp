import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {fetchGeoData} from '../../../state/domain-models/geoData/geoDataActions';
import {fetchSelections} from '../../../state/search/selection/selectionActions';
import {isFetching} from '../../../state/search/selection/selectionSelectors';
import {RowCenter} from '../../common/components/layouts/row/Row';
import {Bold} from '../../common/components/texts/Texts';

interface OwnProps {
  children: React.ReactElement<any> | null;
}

interface StateToProps {
  isFetching: boolean;
}

interface DispatchToProps {
  fetchSearchOptions: () => void;
  fetchGeoData: () => void;
}

type Props = StateToProps & DispatchToProps & OwnProps;

class SelectionOptionsLoaderContainerComponent extends React.Component<Props> {

  componentDidMount() {
    this.props.fetchSearchOptions();
    this.props.fetchGeoData();
  }

  render() {
    const {isFetching} = this.props;
    if (isFetching) {
      return <RowCenter><Bold>...{translate('searching')}</Bold></RowCenter>;
    } else {
      return this.props.children;
    }
  }
}

const mapStateToProps = ({searchParameters}: RootState): StateToProps => {
  return {
    isFetching: isFetching(searchParameters),
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  fetchSearchOptions: fetchSelections,
  fetchGeoData,
}, dispatch);

export const SelectionOptionsLoaderContainer =
  connect<StateToProps, DispatchToProps, {}>
  (mapStateToProps, mapDispatchToProps)(SelectionOptionsLoaderContainerComponent);
