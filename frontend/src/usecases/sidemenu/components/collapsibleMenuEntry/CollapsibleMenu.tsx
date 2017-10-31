import * as React from 'react';
import {DataTree} from '../../containers/organizedData';
import {CollapsibleMenuEntries} from './CollapsibleMenuEntry';
import {Column} from '../../../common/components/layouts/column/Column';

interface DropdownMenuProps {
  data: DataTree[];
  hide: boolean;
}

export const CollapsibleMenu = (props: DropdownMenuProps) => {
  const {data, hide} = props;
  const emptyFunction = () => {
    return null;
  };

  return (
    <Column>
      <CollapsibleMenuEntries
        entry={{childNodes: data}}
        hide={hide}
        level={0}
        notifySelectionChangedToParent={emptyFunction}
      />
    </Column>
  );
};
