import {default as classNames} from 'classnames';
import * as React from 'react';
import {Selectable} from '../../../types/Types';
import {MainMenuItem} from '../../../usecases/main-menu/components/menu-items/MainMenuItem';
import {IconRightArrow} from '../../icons/IconRightArrow';
import {Column} from '../column/Column';
import {Row, RowMiddle} from '../row/Row';
import {FoldableProps} from './Foldable';
import './Foldable.scss';
import {useToggleVisibility} from './foldableHook';
import './FoldableMainMenuItem.scss';

interface Props extends FoldableProps, Required<Selectable> {
  icon: React.ReactElement<any>;
}

export const FoldableMainMenuItem = ({
  icon,
  children,
  className,
  containerClassName,
  fontClassName,
  isSelected,
  title,
  isVisible: initialVisibility = true
}: Props) => {
  const {isVisible, showHide} = useToggleVisibility(initialVisibility);
  const selected = {isSelected};
  const visible = {isVisible};

  return (
    <Column className={classNames('Foldable', containerClassName)}>
      <RowMiddle onClick={showHide} className={classNames('Foldable-title', 'clickable', selected)}>
        <IconRightArrow className={classNames('Foldable-arrow', visible)}/>
        <MainMenuItem name={title} fontClassName={fontClassName} icon={icon}/>
      </RowMiddle>
      <Row className={classNames('Foldable-content', className, visible)}>
        {children}
      </Row>
    </Column>
  );
};
