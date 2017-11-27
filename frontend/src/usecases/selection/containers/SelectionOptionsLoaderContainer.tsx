import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {fetchSelections} from '../../../state/domain-models/domainModelsActions';
import {RowCenter} from '../../common/components/layouts/row/Row';
import {Loading} from '../../common/components/loading/Loading';

interface OwnProps {
  children: React.ReactElement<any> | null;
}

interface StateToProps {
  isFetching: boolean;
}

interface DispatchToProps {
  fetchSelections: () => void;
}

type Props = StateToProps & DispatchToProps & OwnProps;

class SelectionOptionsLoaderContainerComponent extends React.Component<Props> {

  componentDidMount() {
    this.props.fetchSelections();
  }

  render() {
    const {isFetching, children} = this.props;
    if (isFetching) {
      return (<RowCenter><Loading/></RowCenter>);
    } else {
      return children;
    }
  }
}

const mapStateToProps = ({domainModels: {cities, addresses, alarms}}: RootState): StateToProps => {
  return {
    isFetching: cities.isFetching || addresses.isFetching || alarms.isFetching,
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchSelections,
}, dispatch);

export const SelectionOptionsLoaderContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(SelectionOptionsLoaderContainerComponent);
