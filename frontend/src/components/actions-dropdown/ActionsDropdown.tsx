import * as React from 'react';
import {RenderFunction} from '../../types/Types';
import {IconMore} from '../icons/IconMore';
import {PopoverMenu} from '../popover/PopoverMenu';

interface Props {
  renderPopoverContent: RenderFunction;
  className?: string;
}

export const ActionsDropdown = ({renderPopoverContent, className}: Props) => (
  <PopoverMenu className={className} IconComponent={IconMore} renderPopoverContent={renderPopoverContent}/>
);
