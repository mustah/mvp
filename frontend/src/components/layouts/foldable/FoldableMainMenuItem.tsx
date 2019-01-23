import {default as classNames} from 'classnames';
import * as React from 'react';
import {Link} from 'react-router-dom';
import {Clickable, PathNamed} from '../../../types/Types';
import {MainMenuItem} from '../../../usecases/main-menu/components/menu-items/MainMenuItem';
import {IconRightArrow} from '../../icons/IconRightArrow';
import {Column} from '../column/Column';
import {Row, RowMiddle} from '../row/Row';
import {FoldableProps} from './Foldable';
import './Foldable.scss';
import {useToggleVisibility} from './foldableHook';
import './FoldableMainMenuItem.scss';

interface Props extends FoldableProps, PathNamed, Partial<Clickable> {
  icon: React.ReactElement<any>;
  isSelected: boolean;
  linkTo: string;
}

export const FoldableMainMenuItem = ({
  icon,
  children,
  className,
  containerClassName,
  fontClassName,
  isSelected,
  linkTo,
  onClick,
  pathName,
  title,
  isVisible: initialVisibility = true
}: Props) => {
  const {isVisible, showHide} = useToggleVisibility(initialVisibility);
  const selected = {isSelected};
  const visible = {isVisible};

  return (
    <Column className={classNames('Foldable', containerClassName)}>
      <RowMiddle className={classNames('Foldable-title', 'clickable', selected)}>
        <IconRightArrow onClick={showHide} className={classNames('Foldable-arrow', visible)}/>
        <Link to={linkTo} className="link" onClick={onClick}>
          <MainMenuItem
            name={title}
            fontClassName={fontClassName}
            icon={icon}
          />
        </Link>
      </RowMiddle>
      <Row className={classNames('Foldable-content', className, visible)}>
        {children}
      </Row>
    </Column>
  );
};
