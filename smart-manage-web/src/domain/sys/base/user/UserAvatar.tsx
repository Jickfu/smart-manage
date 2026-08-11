import { Avatar, Skeleton } from 'antd';
import { UserOutlined } from '@ant-design/icons';
import { useEffect, useState } from 'react';
import request from '@/api/request';
import './UserAvatar.css';

interface UserAvatarProps {
  name?: string;
  number?: string;
  username?: string;
  src?: string;
  size?: number | 'small' | 'medium' | 'large';
  className?: string;
}

const COLORS = ['#4e79a7', '#f28e2b', '#e15759', '#76b7b2', '#59a14f', '#af7aa1'];

function avatarText(name?: string, number?: string, username?: string) {
  const source = name?.trim() || number?.trim() || username?.trim() || '';
  if (!source) return undefined;
  const chineseCharacters = Array.from(source).filter((character) =>
    /\p{Script=Han}/u.test(character),
  );
  if (chineseCharacters.length > 0) return chineseCharacters.at(-1);
  return Array.from(source)[0]?.toUpperCase();
}

function colorClass(name?: string, number?: string, username?: string) {
  const source = name?.trim() || number?.trim() || username?.trim() || '';
  let hash = 0;
  for (const character of source) hash = (hash * 31 + character.codePointAt(0)!) >>> 0;
  return `sm-user-avatar--color-${hash % COLORS.length}`;
}

export function UserAvatar({
  name,
  number,
  username,
  src,
  size = 'small',
  className,
}: UserAvatarProps) {
  const text = avatarText(name, number, username);
  const [fetchedImage, setFetchedImage] = useState<{
    source: string;
    url?: string;
    failed?: boolean;
  }>();
  const directSrc = src && (src.startsWith('data:') || src.startsWith('blob:')) ? src : undefined;
  const resolvedSrc =
    directSrc ?? (fetchedImage && fetchedImage.source === src ? fetchedImage.url : undefined);
  const remoteSrc = src && !directSrc ? src : undefined;
  const remoteFailed = Boolean(
    remoteSrc && fetchedImage?.source === remoteSrc && fetchedImage.failed,
  );
  useEffect(() => {
    if (!src || src.startsWith('data:') || src.startsWith('blob:')) return;
    let objectUrl: string | undefined;
    let cancelled = false;
    request
      .get<Blob>(src, { responseType: 'blob' })
      .then((response) => {
        if (cancelled) return;
        objectUrl = URL.createObjectURL(response.data);
        setFetchedImage({ source: src, url: objectUrl });
      })
      .catch(() => {
        if (!cancelled) setFetchedImage({ source: src, failed: true });
      });
    return () => {
      cancelled = true;
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [src]);
  if (remoteSrc && !resolvedSrc && !remoteFailed) {
    return <Skeleton.Avatar className={className} active shape="circle" size={size} />;
  }
  return (
    <Avatar
      className={[className, resolvedSrc ? undefined : colorClass(name, number, username)]
        .filter(Boolean)
        .join(' ')}
      src={resolvedSrc}
      icon={text ? undefined : <UserOutlined />}
      size={size}
    >
      {text}
    </Avatar>
  );
}
