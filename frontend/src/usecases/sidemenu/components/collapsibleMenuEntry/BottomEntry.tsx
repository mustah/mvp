import * as classNames from 'classnames';
import * as React from 'react';
import {BaseData} from '../../containers/organizedData';
import {EntryProps} from './CollapsibleMenuEntry';
import {Row} from '../../../common/components/layouts/row/Row';

interface BottomEntryProps extends EntryProps {
  entry: BaseData;
}

export class BottomEntry extends React.Component<BottomEntryProps, any> {

  state = {
    isSelected: this.props.entry.isSelected,
  };

  render() {
    const {entry, level} = this.props;
    return (
      <Row className={classNames('CollapsibleMenuEntry', `CollapsibleMenuEntry-level-${level + 1}`)}>
        {entry.value}
      </Row>
    );
  }
}
