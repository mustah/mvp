import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/index';
import {RowCenter} from '../../common/components/layouts/row/Row';
import {Bold} from '../../common/components/texts/Texts';
import {fetchSelections} from '../../../state/search/selection/selectionActions';
import {isFetching} from '../../../state/search/selection/selectionSelectors';

interface OwnProps {
  children: React.ReactElement<any> | null;
}

interface StateToProps {
  isFetching: boolean;
}

interface DispatchToProps {
  fetchSearchOptions: () => void;
}

type Props = StateToProps & DispatchToProps & OwnProps;

class SelectionOptionsLoaderContainerComponent extends React.Component<Props> {

  componentWillMount() {
    this.props.fetchSearchOptions();
  }

  render() {
    const {isFetching} = this.props;
    if (isFetching) {
      return <RowCenter><Bold>...Söker</Bold></RowCenter>;
    } else {
      return this.props.children;
    }
  }
}

const mapStateToProps = ({selection}: RootState): StateToProps => {
  return {
    isFetching: isFetching(selection),
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  fetchSearchOptions: fetchSelections,
}, dispatch);

export const SelectionOptionsLoaderContainer =
  connect<StateToProps, DispatchToProps, {}>
  (mapStateToProps, mapDispatchToProps)(SelectionOptionsLoaderContainerComponent);
