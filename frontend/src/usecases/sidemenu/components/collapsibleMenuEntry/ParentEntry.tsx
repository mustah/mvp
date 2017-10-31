import * as classNames from 'classnames';
import * as React from 'react';
import {DataTree} from '../../containers/organizedData';
import {CircleMarker, CollapsibleMenuEntries, EntryProps} from './CollapsibleMenuEntry';
import {Column} from '../../../common/components/layouts/column/Column';
import {Row} from '../../../common/components/layouts/row/Row';
import ContentRemoveCircleOutline from 'material-ui/svg-icons/content/remove-circle-outline';
import ContentAddCircleOutline from 'material-ui/svg-icons/content/add-circle-outline';
import AvPlaylistAdd from 'material-ui/svg-icons/av/playlist-add';
import HardwareKeyboardArrowDown from 'material-ui/svg-icons/hardware/keyboard-arrow-down';
import HardwareKeyboardArrowRight from 'material-ui/svg-icons/hardware/keyboard-arrow-right';
import {IconPlaylistRemove} from '../../../common/components/icons/IconPlaylistRemove';

interface ParentEntryProps extends EntryProps {
  entry: DataTree;
}

interface ParentEntryState {
  isOpen: boolean;
  isSelected: boolean;
  childNodesWithSelections: string[];
}

export class ParentEntry extends React.Component<ParentEntryProps, ParentEntryState> {

  state = {
    isOpen: this.props.entry.isOpen,
    isSelected: this.props.entry.isSelected,
    childNodesWithSelections: this.props.entry.childNodesWithSelections,
  };

  toggleShowChildNodes = () => {
    this.setState({isOpen: !this.state.isOpen});
  }

  handleChangeChildrenWithSelections = (id: string) => {
    const indexOfChild = this.state.childNodesWithSelections.indexOf(id);
    const prevNrOfChilds = this.state.childNodesWithSelections.length;
    if (indexOfChild > -1) {
      const updatedChildNodesWithSelection = [...this.state.childNodesWithSelections];
      updatedChildNodesWithSelection.splice(indexOfChild, 1);
      this.setState({
        childNodesWithSelections: updatedChildNodesWithSelection,
      }, () => this.onNotifyParent(this.state.isSelected, prevNrOfChilds));
    } else {
      this.setState({
        childNodesWithSelections: [...this.state.childNodesWithSelections, id],
      }, () => this.onNotifyParent(this.state.isSelected, prevNrOfChilds));
    }
  }

  onNotifyParent = (prevIsSelected: boolean, prevNrOfChilds: number) => {
    const {isSelected, childNodesWithSelections} = this.state;
    const {notifySelectionChangedToParent, entry} = this.props;
    if ((prevIsSelected !== isSelected && childNodesWithSelections.length === 0) ||
      (childNodesWithSelections.length > 0 && prevNrOfChilds === 0 && !isSelected) ||
      (childNodesWithSelections.length === 0 && prevNrOfChilds > 0 && !isSelected)) {
      notifySelectionChangedToParent(entry.value);
    }
  }

  onClick = () => {
    const prevIsSelected = this.state.isSelected;
    this.setState({
      isSelected: !this.state.isSelected,
    }, () => this.onNotifyParent(prevIsSelected, this.state.childNodesWithSelections.length));

  }

  render() {
    const {entry, level} = this.props;
    const {isSelected} = this.state;
    const navigationIcon = this.state.isOpen ? <HardwareKeyboardArrowDown onClick={this.toggleShowChildNodes}/> : <HardwareKeyboardArrowRight onClick={this.toggleShowChildNodes}/>;
    const hasSelectedDescendants = this.state.childNodesWithSelections.length > 0;
    return (
      <Column className={'selected-children-check'}>
        <Row className={classNames('CollapsibleMenuEntry', `CollapsibleMenuEntry-level-${level}`)}>
          {navigationIcon}
          {entry.value}
          <CircleMarker isSelected={isSelected} hasSelectedDescendants={hasSelectedDescendants}/>
          <AvPlaylistAdd
            className={classNames('HoverIcon', {showIcon: true})}
            onClick={() => {}}
          />
          <IconPlaylistRemove
            color="currentcolor"
            name={'playlist-remove'}
            className={classNames('HoverIcon', {showIcon: true})}
            onClick={() => {}}
          />
          <ContentAddCircleOutline
            className={classNames('HoverIcon', {showIcon: !isSelected})}
            onClick={this.onClick}
          />
          <ContentRemoveCircleOutline
            className={classNames('HoverIcon', {showIcon: isSelected})}
            onClick={this.onClick}
          />
        </Row>
        <CollapsibleMenuEntries
          entry={entry}
          level={level + 1}
          hide={!this.state.isOpen}
          notifySelectionChangedToParent={this.handleChangeChildrenWithSelections}
        />
      </Column>
    );
  }
}
