import * as classNames from 'classnames';
import * as React from 'react';
import {BaseData, DataTree} from '../../containers/organizedData';
import {BottomEntry} from './BottomEntry';
import './CollapsibleMenuEntry.scss';
import {ParentEntry} from './ParentEntry';
import {Row} from '../../../common/components/layouts/row/Row';

export interface EntryProps {
  level: number;
  notifySelectionChangedToParent: (id: string) => void;
}

interface CollapsibleMenuEntriesProps extends EntryProps {
  entry: DataTree;
  hide: boolean;
}

export const CollapsibleMenuEntries = (props: CollapsibleMenuEntriesProps) => {
  const {entry, level, hide, notifySelectionChangedToParent} = props;

  const renderChildren = () => (
    entry.childNodes.map((child) => (
      <CollapsibleMenuEntry
        entry={child}
        level={level}
        key={child.value}
        notifySelectionChangedToParent={notifySelectionChangedToParent}
      />
    )));
  return (
    <div className={classNames({hide})}>
      {renderChildren()}
    </div>
  );
};

interface CollapsibleMenuEntryProps extends EntryProps {
  entry: BaseData | DataTree;
}

const CollapsibleMenuEntry = (props: CollapsibleMenuEntryProps) => {
  const {entry, level, notifySelectionChangedToParent} = props;
  return entry.hasOwnProperty('childNodes')
    ? (
      <ParentEntry
        entry={entry as DataTree}
        level={level}
        notifySelectionChangedToParent={notifySelectionChangedToParent}
      />)
    : (
      <BottomEntry
        entry={entry as BaseData}
        level={level}
        notifySelectionChangedToParent={notifySelectionChangedToParent}
      />);
};

interface CircleMarkerProps {
  isSelected: boolean;
  hasSelectedDescendants: boolean;
}

export const CircleMarker = (props: CircleMarkerProps) => {
  const {isSelected, hasSelectedDescendants} = props;
  return (
    <Row className={classNames('CircleMarker', {isSelected}, {hasSelectedDescendants})}>
      <div/>
    </Row>
  );
};
