export interface HalLink {
  href: string;
  templated?: boolean;
}

export type HalLinks = Record<string, HalLink>;

export interface AuthResponse {
  token: string;
  roles: string[];
  username?: string;
  tokenType?: string;
  expiresIn?: number;
  playerId?: number;
  email?: string;
}

export interface IdentityResponse {
  id: number | string;
  uri: string;
  username: string;
  email: string;
  createdAt: string;
  roles: string[];
  _links?: HalLinks;
}

export interface Game {
  id: string;
  uri?: string;
  title: string;
  description?: string;
  category: string;
  thumbnailUrl: string;
  gameUrl: string;
  isActive: boolean;
  _links?: HalLinks;
}

export interface Score {
  id?: number;
  uri?: string;
  scoreValue: number;
  achievedAt?: string;
  player?: string;
  game?: string;
  _links?: HalLinks;
}

export interface GameState {
  id?: number;
  uri?: string;
  payload: string;
  updatedAt?: string;
  player?: string;
  game?: string;
  _links?: HalLinks;
}

export interface ApiError {
  message: string;
}

export interface HalCollection<K extends string, T> {
  _embedded?: Record<K, T[]>;
  _links?: HalLinks;
  page?: {
    size: number;
    totalElements: number;
    totalPages: number;
    number: number;
  };
}
