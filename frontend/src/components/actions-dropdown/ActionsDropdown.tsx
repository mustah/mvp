import * as React from 'react';
import {OnClick, RenderFunction} from '../../types/Types';
import {IconMore} from '../icons/IconMore';
import {PopoverMenu} from '../popover/PopoverMenu';

interface Props {
  renderPopoverContent: RenderFunction<OnClick>;
  className?: string;
}

export const ActionsDropdown = ({renderPopoverContent, className}: Props) => (
  <PopoverMenu className={className} IconComponent={IconMore} renderPopoverContent={renderPopoverContent}/>
);
