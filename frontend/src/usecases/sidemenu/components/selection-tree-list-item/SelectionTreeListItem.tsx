import * as React from 'react';
import {listItemStyle, nestedListItemStyle, sideBarStyles} from '../../../../app/themes';
import {SelectionTreeData} from '../../../../state/domain-models/meter/meterModels';
import {OnClickWithId, uuid} from '../../../../types/Types';
import {SelectableListItem} from './SelectableListItem';

interface RenderSelectionTreeFunctionProps {
  id: uuid;
  data: SelectionTreeData;
  level: string;
  toggleExpand: OnClickWithId;
  toggleSelect: OnClickWithId;
  openListItems: Set<uuid>;
  selectedListItems: Set<uuid>;
}

export const renderSelectionTree = (props: RenderSelectionTreeFunctionProps) => {
  const {id, data, level, toggleExpand, openListItems, toggleSelect, selectedListItems} = props;
  const entity = data.entities[level][id];
  const selectable = entity.selectable;

  const renderChildNodes = (id: uuid) => renderSelectionTree({
    id,
    data,
    level: entity.childNodes.type,
    toggleExpand,
    openListItems,
    toggleSelect,
    selectedListItems,
  });
  const nestedItems = entity.childNodes.ids.sort().map(renderChildNodes);

  const onToggleExpand = () => toggleExpand(entity.id);
  const onToggleSelect = () => toggleSelect(entity.id);
  const PrimaryText = <div style={listItemStyle.textStyle}>{entity.name}</div>;

  return (
    <SelectableListItem
      className="TreeListItem"
      primaryText={PrimaryText}
      key={id}
      innerDivStyle={sideBarStyles.padding}
      initiallyOpen={openListItems.has(entity.id)}
      nestedItems={nestedItems}
      nestedListStyle={nestedListItemStyle}
      onNestedListToggle={onToggleExpand}
      onClick={selectable ? onToggleSelect : () => null}
      selectable={selectable}
      selected={selectedListItems.has(entity.id)}
    />
  );
};
