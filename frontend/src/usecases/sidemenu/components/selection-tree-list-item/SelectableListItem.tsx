import {ListItem} from 'material-ui';
import * as React from 'react';
import {listItemStyle, sideBarStyles} from '../../../../app/themes';
import ListItemProps = __MaterialUI.List.ListItemProps;

interface Selectable {
  selectable: boolean;
}

export const SelectableListItem = (props: ListItemProps & Selectable) => {
  const {selectable, ...listItemProps} = props;
  const selectableStyle: React.CSSProperties = selectable ? {} : sideBarStyles.notSelectable;
  const listStyle: React.CSSProperties = {...listItemStyle, ...selectableStyle};

  return (
    <ListItem
      {...listItemProps}
      style={listStyle}
      hoverColor={sideBarStyles.onHover.color}
    />
  );
};
